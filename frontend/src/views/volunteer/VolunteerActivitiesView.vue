<template>
  <div>
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :items="activities"
        :search="search"
        disable-pagination
        :hide-default-footer="true"
        :mobile-breakpoint="0"
        data-cy="volunteerActivitiesTable"
      >
        <template v-slot:top>
          <v-card-title>
            <v-text-field
              v-model="search"
              append-icon="search"
              label="Search"
              class="mx-2"
            />
            <v-spacer />
          </v-card-title>
        </template>
        <template v-slot:[`item.themes`]="{ item }">
          <v-chip v-for="theme in item.themes" v-bind:key="theme.id">
            {{ theme.completeName }}
          </v-chip>
        </template>
        <template v-slot:[`item.action`]="{ item }">
          <v-tooltip v-if="item.state === 'APPROVED'" bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                color="red"
                v-on="on"
                data-cy="reportButton"
                @click="reportActivity(item)"
                >warning</v-icon
              >
            </template>
            <span>Report Activity</span>
          </v-tooltip>
          <v-tooltip
            v-if="
              hasVolunteerParticipatedInActivity(item) &&
              hasActivityEnded(item) &&
              !hasVolunteerAssessedInstitution(item)
            "
            bottom
          >
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                color="blue"
                v-on="on"
                data-cy="writeAssessmentButton"
                @click="writeAssessment(item)"
                >fas fa-edit</v-icon
              >
            </template>
            <span>Write Assessment</span>
          </v-tooltip>
          <v-tooltip
            v-if="
              hasApplicationDeadlinePassed(item) &&
              !hasVolunteerAlreadyEnrolled(item)
            "
            bottom
          >
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                color="blue"
                v-on="on"
                data-cy="applyForActivityButton"
                @click="applyForActivity(item)"
                >fas fa-sign-in-alt</v-icon
              >
            </template>
            <span>Apply For Activity</span>
          </v-tooltip>
        </template>
      </v-data-table>
      <assessment-dialog
        v-if="currentActivity && assessmentDialog"
        v-model="assessmentDialog"
        :activity="currentActivity"
        v-on:save-assessment="onSaveAssessment"
        v-on:close-assessment-dialog="onCloseAssessmentDialog"
      />
      <enrollment-dialog
        v-if="currentActivity && enrollmentDialog"
        v-model="enrollmentDialog"
        :activity="currentActivity"
        v-on:save-enrollment="onSaveEnrollment"
        v-on:close-enrollment-dialog="onCloseEnrollmentDialog"
      />
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Activity from '@/models/activity/Activity';
import Assessment from '@/models/assessment/Assessment';
import AssessmentDialog from '@/views/volunteer/AssessmentDialog.vue';
import Participation from '@/models/participation/Participation';
import EnrollmentDialog from '@/views/volunteer/EnrollmentDialog.vue';
import Enrollment from '@/models/enrollment/Enrollment';
import { show } from 'cli-cursor';

@Component({
  components: {
    'assessment-dialog': AssessmentDialog,
    'enrollment-dialog': EnrollmentDialog,
  },
  methods: { show },
})
export default class VolunteerActivitiesView extends Vue {
  activities: Activity[] = [];
  assessments: Assessment[] = [];
  participations: Participation[] = [];
  enrollments: Enrollment[] = [];
  search: string = '';

  currentActivity: Activity | null = null;
  assessmentDialog: boolean = false;
  enrollmentDialog: boolean = false;

  headers: object = [
    {
      text: 'Name',
      value: 'name',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Region',
      value: 'region',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Participants',
      value: 'participantsNumberLimit',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Themes',
      value: 'themes',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Description',
      value: 'description',
      align: 'left',
      width: '30%',
    },
    {
      text: 'State',
      value: 'state',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Start Date',
      value: 'formattedStartingDate',
      align: 'left',
      width: '5%',
    },
    {
      text: 'End Date',
      value: 'formattedEndingDate',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Application Deadline',
      value: 'formattedApplicationDeadline',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      sortable: false,
      width: '5%',
    },
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.activities = await RemoteServices.getActivities();
      this.assessments = await RemoteServices.getVolunteerAssessments();
      this.participations = await RemoteServices.getVolunteerParticipations();
      this.enrollments = await RemoteServices.getVolunteerEnrollments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  hasVolunteerAssessedInstitution(activity: Activity) {
    return this.assessments.some(
      (assessment: Assessment) =>
        assessment.institutionId == activity.institution.id,
    );
  }

  hasVolunteerParticipatedInActivity(activity: Activity) {
    return this.participations.some(
      (participation: Participation) => participation.activityId == activity.id,
    );
  }

  hasActivityEnded(activity: Activity) {
    const currentDate = new Date();
    const deadlineDate = new Date(activity.endingDate);

    return currentDate > deadlineDate;
  }

  hasApplicationDeadlinePassed(activity: Activity) {
    const currentDate = new Date();
    const deadlineDate = new Date(activity.applicationDeadline);

    return deadlineDate > currentDate;
  }

  hasVolunteerAlreadyEnrolled(activity: Activity) {
    return this.enrollments.some(
      (enrollment: Enrollment) => enrollment.activity.id == activity.id,
    );
  }

  async reportActivity(activity: Activity) {
    if (activity.id !== null) {
      try {
        const result = await RemoteServices.reportActivity(
          this.$store.getters.getUser.id,
          activity.id,
        );
        this.activities = this.activities.filter((a) => a.id !== activity.id);
        this.activities.unshift(result);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  writeAssessment(activity: Activity) {
    this.currentActivity = activity;
    this.assessmentDialog = true;
  }

  applyForActivity(activity: Activity) {
    this.currentActivity = activity;
    this.enrollmentDialog = true;
  }

  async onSaveAssessment(assessment: Assessment) {
    this.assessments.unshift(assessment);
    this.assessmentDialog = false;
    this.currentActivity = null;
  }

  async onSaveEnrollment(enrollment: Enrollment) {
    this.enrollments.unshift(enrollment);
    this.enrollmentDialog = false;
    this.currentActivity = null;
  }

  onCloseAssessmentDialog() {
    this.assessmentDialog = false;
    this.currentActivity = null;
  }

  onCloseEnrollmentDialog() {
    this.enrollmentDialog = false;
    this.currentActivity = null;
  }
}
</script>

<style lang="scss" scoped></style>
